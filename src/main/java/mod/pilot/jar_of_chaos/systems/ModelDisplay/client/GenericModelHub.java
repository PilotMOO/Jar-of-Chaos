package mod.pilot.jar_of_chaos.systems.ModelDisplay.client;

import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class GenericModelHub {
    private static boolean init = false;
    private static long initStartNano;
    public static void Setup(){
        init = true;
        initStartNano = Util.getNanos();
        System.out.println("[GENERIC MODEL HUB] Init STARTED at " + initStartNano);
        ModelSet = new GenericModelSet();
        _registerInternalGenericModels();
        FinalizeInit();
    }
    public static void FinalizeInit(){
        long nanoDif;
        do {
            nanoDif = Util.getNanos() - initStartNano;
            System.out.println("[GENERIC MODEL HUB] Awaiting end of init...");
            System.out.println("--INFO| Init time elapsed: " + nanoDif);
        } while (nanoDif < 10000L);
        System.out.println("[GENERIC MODEL HUB] Init FINALIZED at " + Util.getNanos());
        init = false;
    }

    private static void _registerInternalGenericModels(){
        System.out.println("Registering internal models...");
        RegisterGenericModel(BeeModel.LAYER_LOCATION, BeeModel::createLayer);
    }

    public static void RegisterGenericModel(ModelLayerLocation location, Supplier<LayerDefinition> supplier) throws IllegalAccessError{
        if (init) ModelSet.addRoot(location, supplier.get());
        else throw new IllegalAccessError("[GENERIC MODEL HUB] ERROR! Attempted to register a generic model AFTER init had finished!" +
                " Ensure you register all of your models BEFORE init is finalized.");
    }
    public static GenericModelSet ModelSet;

    public static class GenericModelSet {
        private final Map<ModelLayerLocation, LayerDefinition> roots = new HashMap<>();
        public void addRoot(ModelLayerLocation key, LayerDefinition value){
            this.roots.put(key, value);
        }

        public ModelPart bakeLayer(ModelLayerLocation pModelLayerLocation) {
            LayerDefinition layerdefinition = this.roots.get(pModelLayerLocation);
            if (layerdefinition == null) {
                throw new IllegalArgumentException("No model for layer " + pModelLayerLocation);
            } else {
                return layerdefinition.bakeRoot();
            }
        }
    }
}
