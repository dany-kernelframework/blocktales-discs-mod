package com.kf.client;

import com.kf.Discs;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class DiscTraderRenderer extends WanderingTraderRenderer {

    private static final Identifier CUSTOM_TEXTURE = Identifier.fromNamespaceAndPath(Discs.MOD_ID, "textures/entity/disc_trader.png");

    public DiscTraderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return CUSTOM_TEXTURE;
    }
}