package pl.com.tt.flex.server.config.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import static pl.com.tt.flex.server.config.jackson.serializers.ViewWithAuthoritySerializerUtil.isObjectComesFromPackage;
import static pl.com.tt.flex.server.config.jackson.serializers.ViewWithAuthoritySerializerUtil.isUserHaveAuthority;

public class ViewWithAuthorityLocalDateTimeSerializer extends LocalDateTimeSerializer {

    /**
     * Pakiety w ktorych sprawdzana jest adnotacja ${@link ViewWithAuthority}
     */
    private final Set<String> packages;

    public ViewWithAuthorityLocalDateTimeSerializer(Set<String> packages) {
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
    public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (isObjectComesFromPackage(packages, g.getCurrentValue()) && !isUserHaveAuthority(value, g)) {
            g.writeNull();
            return;
        }
        // standardowe wykonanie serializacji
        super.serialize(value, g, provider);
    }
}
