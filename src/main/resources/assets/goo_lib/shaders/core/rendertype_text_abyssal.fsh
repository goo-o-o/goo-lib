#version 150

#define PI 3.14159265359

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

float hash(vec2 p) {
    float h = sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123;
    return fract(h);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(mix(hash(i + vec2(0.0,0.0)), hash(i + vec2(1.0,0.0)), u.x),
               mix(hash(i + vec2(0.0,1.0)), hash(i + vec2(1.0,1.0)), u.x), u.y);
}

float fbm(vec2 p) { // fractal brownian motion
    float v = 0.0;
    float a = 0.5;
    vec2 shift = vec2(100.0);
    mat2 rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.50));
    for (int i = 0; i < 4; ++i) {
        v += a * noise(p);
        p = rot * p * 2.0 + shift;
        a *= 0.5;
    }
    return v;
}

void main() {
    float currentA = texture(Sampler0, texCoord).a;

    if (currentA < 0.01) {
        discard;
    }

    vec2 fogCoord = texCoord * 40.0; // bigger = more compact

    vec2 windDirection = vec2(GameTime * 80, GameTime * 20); // wind speed

    float mistDensity = fbm(fogCoord - windDirection);


    float occlusionFactor = smoothstep(0.3, 0.7, mistDensity); // magic
    float activeAlpha = currentA * (1.0 - (occlusionFactor * 0.85));

    vec3 baseTextColor = vertexColor.rgb;
    vec3 thickFogColor = vertexColor.rgb * 0.25; // original but darker, can make black or whatever color

    vec3 finalRGB = mix(baseTextColor, thickFogColor, occlusionFactor * 0.9); // fog strength

    fragColor = vec4(finalRGB, activeAlpha * vertexColor.a) * ColorModulator;

    // sharpen edges
    if (fragColor.a < 0.05) {
        discard;
    }
}