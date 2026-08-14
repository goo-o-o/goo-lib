package com.goo.goo_lib.client.datagen;

import com.goo.goo_lib.client.registry.GLParticles;
import com.goo.goo_lib.common.GooLib;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class GLParticleDescriptionProvider extends ParticleDescriptionProvider {


    /**
     * Creates an instance of the data provider.
     *
     * @param output     the expected root directory the data generator outputs to
     * @param fileHelper the helper used to validate a texture's existence
     */
    public GLParticleDescriptionProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper);
    }

    @Override
    protected void addDescriptions() {
        sprite(GLParticles.TRAIL_PARTICLE.value(), GooLib.loc("generic_dot"));
    }
}