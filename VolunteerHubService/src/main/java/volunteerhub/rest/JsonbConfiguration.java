package volunteerhub.rest;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Configures how JSON-B converts our objects to JSON.
 * <p>
 * Passwords are never sent back to the client: the getter getPassword() is hidden from JSON-B, so a
 * password can still be <i>received</i> (setPassword, e.g. when creating an account) but never
 * <i>sent</i>. This keeps the domain model free of any web annotation.
 */
@Provider
public class JsonbConfiguration implements ContextResolver<Jsonb> {

    private final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
            .withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
                @Override
                public boolean isVisible(Field field) {
                    return Modifier.isPublic(field.getModifiers());
                }

                @Override
                public boolean isVisible(Method method) {
                    return Modifier.isPublic(method.getModifiers()) && !method.getName().equals("getPassword");
                }
            }));

    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }
}
