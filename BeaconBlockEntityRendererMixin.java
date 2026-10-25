package fr.thunderbeacon.mixin;

import fr.thunderbeacon.ThunderBeamRenderer;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconBlockEntityRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BeaconBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void thunderbeacon$replaceBeam(BeaconBlockEntity beacon, float tickDelta, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light, int overlay,
                                           CallbackInfo ci) {
        ThunderBeamRenderer.render(beacon, tickDelta, matrices, vertexConsumers);
        ci.cancel();
    }
}
