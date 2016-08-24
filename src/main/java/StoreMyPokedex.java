import POGOProtos.Data.PokedexEntryOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Pokedex;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

public class StoreMyPokedex {
    public static void main(String[] args) throws LoginFailedException, RemoteServerException, IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Properties properties = new Properties();
        properties.load(new FileReader(System.getProperty("pokebot.properties", "pokebot.properties")));
        PokemonGo go = new PokemonGo(new GoogleUserCredentialProvider(httpClient, properties.getProperty("realAccountGoogleRefreshToken")), httpClient);
        Pokedex pokedex = go.getInventories().getPokedex();
        try (PrintStream out = new PrintStream(FindPokemons.MY_CAPTURED)) {
            for (PokemonIdOuterClass.PokemonId pokemonId : PokemonIdOuterClass.PokemonId.values()) {
                PokedexEntryOuterClass.PokedexEntry pokedexEntry = pokedex.getPokedexEntry(pokemonId);
                if (pokedexEntry != null && pokedexEntry.getTimesCaptured() > 0) {
                    out.println(pokemonId);
                    System.out.println(pokemonId.getNumber()+"\t"+pokemonId);
                }
            }
        }
    }
}
