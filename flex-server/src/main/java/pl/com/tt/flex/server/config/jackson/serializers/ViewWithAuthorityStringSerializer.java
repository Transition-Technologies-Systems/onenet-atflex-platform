package pl.com.tt.flex.server.config.jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import pl.com.tt.flex.model.security.permission.ViewWithAuthority;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static pl.com.tt.flex.server.config.jackson.serializers.ViewWithAuthoritySerializerUtil.isObjectComesFromPackage;
import static pl.com.tt.flex.server.config.jackson.serializers.ViewWithAuthoritySerializerUtil.isUserHaveAuthority;

public class ViewWithAuthorityStringSerializer extends StdScalarSerializer<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Pakiety w ktorych sprawdzana jest adnotacja ${@link ViewWithAuthority}
     */
    private Set<String> packages = new HashSet<>();

    public ViewWithAuthorityStringSerializer(Set<String> packages) {
        super(String.class, false);
        this.packages = packages;
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, String value) {
        return value.length() == 0;
    }

    /**
     * Funkcjonalność ukrywania pol per uprawnienia ograniczona jest do pakietów zdefiniowanych w polu ${@link #packages}
     * <p>
     * Podczas serializacji jeżeli pole ma ustawioną adnotacje ${@link ViewWithAuthority} to sprawdzane jest
     * czy dany użytkownik ma dostęp do tego pola, poprzez sprawdzenie czy dany użytkownik ma uprawnienia które zostały
     * okreslone w adnotacji
     */
    @Override
    public void serialize(String value, JsonGenerator g, SerializerProvider provider) throws IOException {
        if (isObjectComesFromPackage(packages, g.getCurrentValue()) && !isUserHaveAuthority(value, g)) {
            g.writeNull();
            return;
        }
        // standardowe wykonanie serializacji
        g.writeString(value);
    }

    @Override
    public final void serializeWithType(String value, JsonGenerator g, SerializerProvider provider,
                                        TypeSerializer typeSer) throws IOException {
        if (isObjectComesFromPackage(packages, g.getCurrentValue()) && !isUserHaveAuthority(value, g)) {
            g.writeNull();
            return;
        }
        // standardowe wykonanie serializacji
        g.writeString(value);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return createSchemaNode("string", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        visitStringFormat(visitor, typeHint);
    }
}
