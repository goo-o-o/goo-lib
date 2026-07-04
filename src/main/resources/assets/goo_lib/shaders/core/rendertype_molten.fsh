#version 150

// Adjust this scale if the texture looks too big or small on the mob's body
#define MOLTEN_SCALE        2.0
#define TIME_SPEED          500.0
#define CORE_THRESHOLD      0.55
#define SHADOW_FLOOR        0.35

#define COLOR_MOLTEN_CORE   vec4(1.3, 0.95, 0.0, 0.75)
#define COLOR_LIQUID_LAVA   vec4(1.0, 0.25, 0.0, 0.5)
#define COLOR_COOLED_CRUST  vec4(0.12, 0.05, 0.05, 0.0)

#define PI 3.14159265359
#define TWO_PI 6.28318530718

uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec3 localPos; // <-- Received from vertex shader
out vec4 fragColor;

vec3 hueShift(vec3 color, float angle) {
    vec3 k = vec3(0.57735, 0.57735, 0.57735);
    float cosAngle = cos(angle);
    return color * cosAngle + cross(k, color) * sin(angle) + k * dot(k, color) * (1.0 - cosAngle);
}

void main() {
    vec2 magmaUV = localPos.xy * MOLTEN_SCALE;

    float loopTime = mod(GameTime * (TIME_SPEED), TWO_PI);
    vec2 loopMovement = vec2(cos(loopTime), sin(loopTime)) * 2.0;
    vec2 coord = magmaUV + loopMovement;

    float waveFactor = 0.0;
    waveFactor += sin(coord.x * 1.0 + loopTime) * cos(coord.y * 1.0 - loopTime);
    waveFactor += sin(coord.x * 2.0 - loopTime) * cos(coord.y * 2.0 + loopTime) * 0.5;
    waveFactor += sin(coord.x * 4.0 + loopTime * 2.0) * 0.25;
    float magmaHeat = clamp((waveFactor + 1.0) * 0.5, 0.0, 1.0);

    vec4 magmaRGBA;
    if (magmaHeat > CORE_THRESHOLD) {
        magmaRGBA = mix(COLOR_LIQUID_LAVA, COLOR_MOLTEN_CORE, smoothstep(CORE_THRESHOLD, 0.85, magmaHeat));
    } else {
        magmaRGBA = mix(COLOR_COOLED_CRUST, COLOR_LIQUID_LAVA, smoothstep(0.1, CORE_THRESHOLD, magmaHeat));
    }

    vec3 finalRGB = magmaRGBA.rgb;

    float hueRotationAngle = vertexColor.r * TWO_PI;
    finalRGB = hueShift(finalRGB, hueRotationAngle);
    finalRGB *= vertexColor.rgb;

    float baseAlpha = magmaRGBA.a * ColorModulator.a;
    vec4 finalColor = vec4(finalRGB, baseAlpha);
    finalColor.rgb *= ColorModulator.rgb;

    fragColor = finalColor * vertexColor.a;
}