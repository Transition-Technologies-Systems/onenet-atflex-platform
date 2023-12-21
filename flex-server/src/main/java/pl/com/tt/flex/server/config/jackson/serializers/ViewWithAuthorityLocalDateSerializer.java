package pl.com.tt.flex.server.config.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

import static pl.com.tt.flex.server.config.jackson.serializers.ViewWithAuthoritySerializerUtil.isObjectComesFromPackage;
import static pl.com.tt.flex.server.config.jackson.serializers.ViewWithAuthoritySerializerUtil.isUserHaveAuthority;

public class ViewWithAuthorityLocalDateSerializer extends LocalDateSerializer {

    /**
     * Pakiety w ktorych sprawdzana jest adnotacja ${@link ViewWithAuthority}
     */
    private final Set<String> packages;

    public ViewWithAuthorityLocalDateSerializer(Set<String> packages) {
        super();
        this.packages = packages;
    }

    /**
     * Funkcjonalność ukrywania pol per uprawnienia ograniczona jest do pakietów zdefiniowanych w polu ${@link #packages}
     * <p>
     * Podczas serializacji jeżeli pole ma ustawioną adnotacje ${@link ViewWithAuthority} to sprawdzane jest
     * czy dany użytkownik ma dostęp do tego pola, poprzez sprawdzenie czy dany użytkownik ma uprawnienia które zostały
     * okreslone w adnotacji
     */
    @Override
    public void serialize(LocalDate value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (isObjectComesFromPackage(packages, g.getCurrentValue()) && !isUserHaveAuthority(value, g)) {
            g.writeNull();
            return;
        }
        // standardowe wykonanie serializacji
        super.serialize(value, g, provider);
    }
}
