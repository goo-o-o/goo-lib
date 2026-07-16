#version 150

uniform sampler2D Sampler0;
uniform float GameTime;
uniform float Speed;
uniform float Wobble;          // UV wobble strength (was hardcoded 0.001 multiplier)
uniform float ColorMix;        // was hardcoded 0.6 — blend between vertex color and acid color
uniform float HueSpeed;        // was hardcoded 0.03 — how fast the hue cycles over time

in vec4 vertexColor;
in vec2 texCoord0;
in vec2 screenPos;
in float noiseVal;             // renamed to match vertex shader's actual output name

out vec4 fragColor;

#define T (GameTime * 100.0 * Speed)

vec3 hue2rgb(float h) {
    vec3 p = abs(fract(h + vec3(0.0, 1.0/3.0, 2.0/3.0)) * 6.0 - 3.0);
    return clamp(p - 1.0, 0.0, 1.0);
}

void main() {
    // Tiny UV wobble so the texture itself looks like it's melting,
    // not just the quad outline
    vec2 uvWobble = vec2(
            sin(T * 0.8 + screenPos.y * 3.0),
            cos(T * 0.6 + screenPos.x * 3.0)
    ) * 0.001 * Wobble;

    float alpha = texture(Sampler0, texCoord0 + uvWobble).a;
    if (alpha < 0.01) discard;

    float hue = fract(noiseVal * 0.2 + T * HueSpeed + screenPos.x * 0.01 + screenPos.y * 0.01);
    vec3 acidColor = hue2rgb(hue);
    acidColor = pow(acidColor, vec3(0.7));

    vec3 finalRGB = mix(vertexColor.rgb, acidColor, ColorMix);

    fragColor = vec4(finalRGB, alpha * vertexColor.a);
}
