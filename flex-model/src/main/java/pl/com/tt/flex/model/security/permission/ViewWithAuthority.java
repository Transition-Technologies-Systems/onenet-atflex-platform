package pl.com.tt.flex.model.security.permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Adnotacja słuząca do nadawania dostępu do określonych pól
 * dla użytkowników ktorzy mają określone uprawnienia
 *
 * Aby adnotacja była brana podczas serializacji należy nadpisać klase serializującą dla
 * danego typu obiektu. Przyklad dla NumberSerializer:
 *
 * public class ViewWithAuthorityNumberSerializer extends NumberSerializer {
 *
 * 	public ViewWithAuthorityNumberSerializer(Class<? extends Number> rawType) {
 * 		super(rawType);
 *      }
 *
 * 	public final static ViewWithAuthorityNumberSerializer instance = new ViewWithAuthorityNumberSerializer(Number.class);
 *
 *   @Override
 *   public void serialize(Number value, JsonGenerator g, SerializerProvider provider) throws IOException {
 * 		if (Objects.nonNull(value) && Objects.nonNull(g.getOutputContext().getCurrentName())) {
 * 			String currentName = g.getOutputContext().getCurrentName();
 * 			Field field = findField(currentName, g.getCurrentValue());
 * 			if (Objects.nonNull(field) && field.isAnnotationPresent(ViewWithAuthority.class)) {
 * 				String[] authorities = field.getAnnotation(ViewWithAuthority.class).values();
 * 				if (Arrays.stream(authorities).noneMatch(SecurityUtils::isCurrentUserInAuthority)) {
 * 					g.writeNull();
 * 					return;
 *            }
 *         }
 *      }
 * 		super.serialize(value, g, provider);
 *   }
 *
 * 	protected Field findField(String fieldName, Object o) {
 * 		Class<?> current = o.getClass();
 * 		do {
 * 			try {
 * 				return current.getDeclaredField(fieldName);
 *         } catch (Exception e) {
 *         }
 *      } while ((current = current.getSuperclass()) != null);
 * 		return null;
 *   }
 * }
 *
 * Należy także dodać serializer do konfiguracji Jackson'a:
 *
 *     @Bean
 *     public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
 *         return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
 *             .serializerByType(Number.class, ViewWithAuthorityNumberSerializer.instance);
 *     }
 */
@Target({FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewWithAuthority {

	String[] values();

	boolean ignoreInXlsxExport() default false;
}