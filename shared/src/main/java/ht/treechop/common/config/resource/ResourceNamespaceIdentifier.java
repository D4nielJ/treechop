package ht.treechop.common.config.resource;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.stream.Stream;

public class ResourceNamespaceIdentifier extends ResourceIdentifier {

    public ResourceNamespaceIdentifier(String namespace, List<IdentifierQualifier> qualifiers, String string) {
        super(namespace, "", qualifiers, string);
    }

    @Override
    public <R extends DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
        return registry.stream()
                .filter(resource -> {
                    Identifier loc = registry.getKey(resource);
                    return loc != registry.getDefaultKey() && loc.getNamespace().equals(getNamespace());
                });
    }

}
