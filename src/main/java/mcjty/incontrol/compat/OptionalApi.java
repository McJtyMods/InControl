package mcjty.incontrol.compat;

import mcjty.incontrol.InControl;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/** Calls optional mod APIs without linking their loader-specific dependencies. */
final class OptionalApi {
    private static final Set<String> reported = new HashSet<>();
    private OptionalApi() {}
    static Object invoke(Object target, String name, Object... args) {
        if (target == null) return null;
        Class<?> type = target instanceof Class<?> c ? c : target.getClass();
        try {
            for (Method method : type.getMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
                boolean matches = true;
                Class<?>[] params = method.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    Class<?> param = params[i] == int.class ? Integer.class : params[i];
                    if (args[i] != null && !param.isInstance(args[i])) matches = false;
                }
                if (matches) return method.invoke(target instanceof Class<?> ? null : target, args);
            }
            throw new NoSuchMethodException(name);
        } catch (ReflectiveOperationException | RuntimeException e) {
            if (reported.add(type.getName() + "." + name)) InControl.setup.getLogger().warn("Optional API unavailable: " + type.getName() + "." + name, e);
            return null;
        }
    }
    static Class<?> type(String name) {
        try { return Class.forName(name); }
        catch (ClassNotFoundException | LinkageError e) { return null; }
    }
}
