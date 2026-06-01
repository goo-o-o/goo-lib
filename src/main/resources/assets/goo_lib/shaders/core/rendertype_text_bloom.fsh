#version 150

#define PI 3.14159265359

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord;
in vec3 glyphT0;
in vec3 glyphT1;
in vec3 glyphT2;
in vec3 glyphT3;

out vec4 fragColor;

void main() {
    float intensity = 2.0;
    float radius = 1.0;

    // calculate uv bounds
    vec2 uvMin = vec2(100.0);
    vec2 uvMax = vec2(-100.0);
    if (glyphT0.z > 0.001) { vec2 p = glyphT0.xy / glyphT0.z; uvMin = min(uvMin, p); uvMax = max(uvMax, p); }
    if (glyphT1.z > 0.001) { vec2 p = glyphT1.xy / glyphT1.z; uvMin = min(uvMin, p); uvMax = max(uvMax, p); }
    if (glyphT2.z > 0.001) { vec2 p = glyphT2.xy / glyphT2.z; uvMin = min(uvMin, p); uvMax = max(uvMax, p); }
    if (glyphT3.z > 0.001) { vec2 p = glyphT3.xy / glyphT3.z; uvMin = min(uvMin, p); uvMax = max(uvMax, p); }

    if (uvMax.x < uvMin.x || uvMax.y < uvMin.y) {
        uvMin = vec2(0.0);
        uvMax = vec2(1.0);
    }

    vec2 texSize = vec2(textureSize(Sampler0, 0));
    float currentA = texture(Sampler0, texCoord).a;
    float bloom = 0.0;
    const int SAMPLES = 50;

    for (int i = 0; i < SAMPLES; i++) {
        float angle = float(i) * (PI * 2.0 / float(SAMPLES));

        for (float r = 1.0; r <= 4.0; r += 1.0) {
            vec2 offset = vec2(cos(angle), sin(angle)) * (r * radius / texSize);
            vec2 sampleUV = texCoord + offset;

            if (sampleUV.x < uvMin.x || sampleUV.x > uvMax.x ||
            sampleUV.y < uvMin.y || sampleUV.y > uvMax.y) {
                continue;
            }

            float sampleA = texture(Sampler0, sampleUV).a;
            bloom += sampleA / (r * 0.8);
        }
    }

    bloom /= float(SAMPLES) * 4.0;
    bloom = min(bloom * intensity, 1.0);

    if (currentA > 0.1) { // checks if theres base text here
        fragColor = vec4(vertexColor.rgb, currentA * vertexColor.a) * ColorModulator;
    } else if (bloom > 0.08) {
        fragColor = vec4(vertexColor.rgb, bloom * vertexColor.a * 0.85) * ColorModulator;
    } else {
        discard;
    }
}