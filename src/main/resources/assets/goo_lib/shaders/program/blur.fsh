#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;
uniform vec2 BlurDir;
uniform float Radius;

out vec4 fragColor;

void main() {
    vec3 blurredColor = vec3(0.0);
    float totalStrength = 0.0;
    float blendedAlpha = 0.0;
    float rad = Radius;

    for(float r = -rad; r <= rad; r += 1.0) {
        vec4 sampleValue = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);

        // Standard Gaussian color weight
        float strength = 1.0 - abs(r / rad);
        totalStrength += strength;
        blurredColor += sampleValue.rgb * strength;

        // Cumulative Alpha: If ANY sampled neighborhood pixel has opacity,
        // accumulate it heavily so the glow spreads wide.
        blendedAlpha += sampleValue.a * strength;
    }

    // Normalize color against weight, and clamp alpha smoothly between 0.0 and 1.0
    fragColor = vec4(blurredColor / totalStrength, min(blendedAlpha, 1.0));
}