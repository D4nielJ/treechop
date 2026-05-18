package ht.treechop.client.model;

import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelLoadingPlugin implements ModelLoadingPlugin {
    private final Supplier<ChoppedLogBakedModel> modelSupplier;

    public ChoppedLogModelLoadingPlugin(Supplier<ChoppedLogBakedModel> modelSupplier) {
        this.modelSupplier = modelSupplier;
    }

    @Override
    public void initialize(Context pluginContext) {
        ChoppedLogBakedModel model = modelSupplier.get();
        pluginContext.modifyBlockModelAfterBake().register((original, context) -> {
            if (context.state().is(FabricModBlocks.CHOPPED_LOG)) {
                model.setWrapped(original);
                return model;
            }
            return original;
        });
    }
}

