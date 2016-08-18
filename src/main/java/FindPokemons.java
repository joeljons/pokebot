import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.device.DeviceInfo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import static java.nio.file.Files.lines;
import static java.nio.file.Paths.get;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;

public class FindPokemons {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileReader("pokebot.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String SLACK_TOKEN = properties.getProperty("slack.bottoken");

    private static final double LATITUDE = Double.valueOf(properties.getProperty("latitude"));
    private static final double LONGITUDE = Double.valueOf(properties.getProperty("longitude"));
    private static final int POKEMON_SPAM_LIMIT = Integer.valueOf(properties.getProperty("spamlimit"));
    private static final String SLACK_CHANNEL = properties.getProperty("slack.channel");
    private static final String MY_SLACK_IM_CHANNEL = properties.getProperty("slack.imchannel");
    private static final String LOGFILE = "logfile.txt";
    static final String MY_CAPTURED = "my_captured.txt";
    private static Random random = new Random();

    @SuppressWarnings({"Duplicates", "InfiniteLoopStatement"})
    public static void main(String[] args) throws InterruptedException, LoginFailedException, RemoteServerException, IOException {


        CloseableHttpClient httpclient = HttpClients.createDefault();

        Set<PokemonId> capturedPokemons = null;
        if (new File(MY_CAPTURED).exists()) {
            capturedPokemons = lines(get(MY_CAPTURED)).map(PokemonId::valueOf).collect(toSet());
        }

        OkHttpClient httpClient = new OkHttpClient();

        Map<PokemonId, Integer> seenPokemons = new HashMap<>();
        Set<Long> seenEncounters = new HashSet<>();
        if (new File(LOGFILE).exists()) {
            lines(get(LOGFILE))
                    .map(line -> PokemonId.valueOf(line.split("\t")[0]))
                    .forEach(pokemonId -> seenPokemons.put(pokemonId, seenPokemons.getOrDefault(pokemonId, 0) + 1));

            seenPokemons.entrySet().stream()
                    .sorted(reverseOrder(comparing(Map.Entry::getValue)))
                    .map(entry -> entry.getValue() + "\t" + entry.getKey())
                    .forEach(System.out::println);

            lines(get(LOGFILE))
                    .map(line -> Long.valueOf(line.split("\t")[4]))
                    .forEach(seenEncounters::add);
        }


        PrintStream pokelog = new PrintStream(new FileOutputStream(LOGFILE, true), true);

        PokemonGo go = createGo(httpClient);

        while (true) {
            if (Boolean.valueOf(properties.getProperty("officebot")) && outOfOffice()) {
                System.out.println("Zzzzzzzzz");
                do {
                    Thread.sleep(1000 * 60);
                } while (outOfOffice());
            }
            List<CatchablePokemon> catchablePokemon = emptyList();
            try {
                catchablePokemon = go.getMap().getCatchablePokemon();
                System.out.println("catchablePokemon.size() = " + catchablePokemon.size());
            } catch (LoginFailedException e) {
                e.printStackTrace();
                System.out.println("Try to login again");
                go = createGo(httpClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (CatchablePokemon pokemon : catchablePokemon) {
                System.out.println("pokemon = " + pokemon);
                if (!seenEncounters.add(pokemon.getEncounterId())) {
                    System.out.println("Already seen this encounter");
                    continue;
                }
                try {
                    PokemonId pokemonId = pokemon.getPokemonId();
                    seenPokemons.put(pokemonId, seenPokemons.getOrDefault(pokemonId, 0) + 1);
                    pokelog.println(String.format(Locale.ENGLISH, "%s\t%d\t%f\t%f\t%d",
                            pokemonId,
                            pokemon.getExpirationTimestampMs(),
                            pokemon.getLatitude(),
                            pokemon.getLongitude(),
                            pokemon.getEncounterId()));

                    String expiration = new SimpleDateFormat("HH:mm:ss").format(new Date(pokemon.getExpirationTimestampMs()));
                    long secondsLeft = (pokemon.getExpirationTimestampMs() - new Date().getTime()) / 1000;
                    String message = String.format(Locale.ENGLISH, "%s expires in %d s (%s) %d m %s",
                            pokemonId,
                            secondsLeft,
                            expiration,
                            (int) getDistance(pokemon),
                            getBearingString(pokemon));

                    if (capturedPokemons != null && capturedPokemons.add(pokemonId) && MY_SLACK_IM_CHANNEL == null) {
                        sendMessage(httpclient, MY_SLACK_IM_CHANNEL, "Gotta catch em all!!!!!!!\n" + message);
                    }

                    if (seenPokemons.getOrDefault(pokemonId, 0) > POKEMON_SPAM_LIMIT) {
                        System.out.println("SpamAlert! Already seen " + pokemonId + " " + seenPokemons.get(pokemonId) + " times");
                        continue;
                    }

                    System.out.println(message);
                    sendMessage(httpclient, SLACK_CHANNEL, message);
                    if (seenPokemons.get(pokemonId) >= POKEMON_SPAM_LIMIT) {
                        sendMessage(httpclient, SLACK_CHANNEL, "Sorry for spamming about " + pokemonId + " all the time. I'll ignore them from now on.");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            Thread.sleep(1000 * 30 + random.nextInt(1000 * 90));
        }
    }

    private static boolean outOfOffice() {
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY || hour < 8 || hour >= 18;
    }

    private static void sendMessage(CloseableHttpClient httpclient, String slackChannel, String message) throws URISyntaxException, IOException {
        URI uri = new URIBuilder("https://slack.com/api/chat.postMessage")
                .setCharset(StandardCharsets.UTF_8)
                .setParameter("token", SLACK_TOKEN)
                .setParameter("text", message)
                .setParameter("channel", slackChannel)
                .setParameter("as_user", "true")
                .build();
        HttpGet httpGet = new HttpGet(uri);

        httpclient.execute(httpGet).close();
    }

    private static double getDistance(CatchablePokemon pokemon) {
        Point point = new Point(new DegreeCoordinate(pokemon.getLatitude()), new DegreeCoordinate(pokemon.getLongitude()));
        Point home = new Point(new DegreeCoordinate(LATITUDE), new DegreeCoordinate(LONGITUDE));
        return EarthCalc.getDistance(home, point);
    }

    private static String getBearingString(CatchablePokemon pokemon) {
        Point point = new Point(new DegreeCoordinate(pokemon.getLatitude()), new DegreeCoordinate(pokemon.getLongitude()));
        Point home = new Point(new DegreeCoordinate(LATITUDE), new DegreeCoordinate(LONGITUDE));
        return BearingFinder.getBearingString(home, point);
    }

    private static PokemonGo createGo(OkHttpClient httpClient) throws LoginFailedException, RemoteServerException, InterruptedException {
        PokemonGo go = new PokemonGo(new PtcCredentialProvider(httpClient, properties.getProperty("ptc.username"), properties.getProperty("ptc.password")), httpClient);
        DeviceInfo deviceInfo = new DeviceInfo();

        deviceInfo.setDeviceId("A85C6E556A5355265653DE32354FA85C6E556A5355265653DE32354F5FDDDFA3");
        deviceInfo.setDeviceBrand("Apple");

        deviceInfo.setDeviceModel("iPhone");
        deviceInfo.setDeviceModelBoot("iPhone7,2");
        deviceInfo.setHardwareManufacturer("Apple");
        deviceInfo.setHardwareModel("N61AP");
        deviceInfo.setFirmwareBrand("iPhone OS");
        deviceInfo.setFirmwareType("9.3.4");

        go.setDeviceInfo(deviceInfo);


        go.setLocation(LATITUDE, LONGITUDE, 1);

        Thread.sleep(2000 + random.nextInt(5000));

        return go;
    }
}
