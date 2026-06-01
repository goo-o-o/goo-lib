package com.goo.goo_lib.utils.text;

import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;

import java.util.List;

public interface StyleEffectContainer {
    List<ConfiguredEffect<?>> gl$getEffects();
    void gl$setEffects(List<ConfiguredEffect<?>> effects);
}