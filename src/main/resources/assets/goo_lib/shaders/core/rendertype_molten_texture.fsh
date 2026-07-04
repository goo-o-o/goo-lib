#version 150

// size of the magma pockets
#define MOLTEN_SCALE        5.5
#define TIME_SPEED          500.0
#define CORE_THRESHOLD      0.55
#define SHADOW_FLOOR        0.35

// movement vector
#define MOVEMENT_VECTOR     vec2(- 0.2, 0.4)

// palette
#define COLOR_MOLTEN_CORE   vec4(1.3, 0.95, 0.0, 0.75)
#define COLOR_LIQUID_LAVA   vec4(1.0, 0.25, 0.0, 0.5)
#define COLOR_COOLED_CRUST  vec4(0.12, 0.05, 0.05, 0.0)

// to ensure seamless looping
#define PI 3.14159265359
#define TWO_PI 6.28318530718

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Helper function to hue shift an RGB color by a given radian angle
vec3 hueShift(vec3 color, float angle) {
    vec3 k = vec3(0.57735, 0.57735, 0.57735);
    float cosAngle = cos(angle);
    return color * cosAngle + cross(k, color) * sin(angle) + k * dot(k, color) * (1.0 - cosAngle);
}

void main() {
    vec4 texSample = texture(Sampler0, texCoord0);

    if (texSample.a < 0.01) {
        discard;
    }

    vec2 currentTexSize = vec2(textureSize(Sampler0, 0));
    vec2 pixelatedUV = floor(texCoord0 * currentTexSize) / currentTexSize;
    vec2 magmaUV = pixelatedUV * currentTexSize * (1.0 / MOLTEN_SCALE);

    // use a clean time variable that loops back
    float loopTime = mod(GameTime * (TIME_SPEED), TWO_PI);

    // make it go in a circle
    vec2 loopMovement = vec2(cos(loopTime), sin(loopTime)) * 2.0;
    vec2 coord = magmaUV + loopMovement;

    // Fixed internal wave functions to complete full rotations
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

    // Apply Hue Shifting
    float hueRotationAngle = vertexColor.r * TWO_PI;
    finalRGB = hueShift(finalRGB, hueRotationAngle);
    finalRGB *= vertexColor.rgb;

    float baseAlpha = texSample.a * magmaRGBA.a * ColorModulator.a;
    vec4 finalColor = vec4(finalRGB, baseAlpha);

    finalColor.rgb *= ColorModulator.rgb;

    fragColor = finalColor * vertexColor.a;

}