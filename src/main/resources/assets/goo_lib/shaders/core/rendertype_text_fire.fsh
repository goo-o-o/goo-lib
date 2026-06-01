#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime; // Driven by your smoothly updating Java uniform

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    // 1. Fetch base text texture sample
    vec4 textSample = texture(Sampler0, texCoord);

    // Early discard on empty space to keep font atlas boundaries perfectly clean
    if (textSample.a < 0.01) {
        discard;
    }

    // 2. Local UV Scaling for fire pattern size
    vec2 fireUV = texCoord * 75.0;

    // 3. Procedural Flame Math (Trigonometric Noise Network)
    float timeSource = GameTime * 2000.0;
    vec2 movement = vec2(0.0, timeSource);
    vec2 coord = fireUV + movement;

    float waveFactor = 0.0;
    waveFactor += sin(coord.x * 1.5 + timeSource) * cos(coord.y * 1.0 - timeSource);
    waveFactor += sin(coord.x * 3.1 + timeSource * 1.5) * cos(coord.y * 2.3 + timeSource * 0.7) * 0.5;
    waveFactor += sin(coord.x * 6.2 - timeSource * 2.0) * 0.25;

    // Normalize wave factor to a positive 0.0 -> 1.0 range
    float fireIntensity = clamp((waveFactor + 1.0) * 0.5, 0.0, 1.0);

    // 4. EXTRACT ORIGINAL BRIGHTNESS (LUMINANCE)
    // Uses standard digital video coefficients to calculate accurate human-perceived brightness
    float originalBrightness = dot(vertexColor.rgb, vec3(0.2126, 0.7152, 0.0722));

    // Smooth and clamp the brightness multiplier so it doesn't clip to absolute zero or overflow
    float brightnessMultiplier = clamp(originalBrightness, 0.15, 1.0);

    // 5. Base Color Palette Gradients
    vec3 innerCoreColor = vec3(1.0, 0.65, 0.0);  // Bright Flame Orange
    vec3 outerEdgeColor = vec3(0.9, 0.15, 0.0);  // Deep Crimson Embers

    // Blend the fire colors together based on noise weight
    vec3 activeFireRGB = mix(outerEdgeColor, innerCoreColor, smoothstep(0.3, 0.8, fireIntensity));

    // 6. APPLY BRIGHTNESS TINT
    // Multiply the final fire RGB by our extracted brightness multiplier.
    // For the main text, this multiplies by ~1.0 (no change).
    // For the shadow, this multiplies by ~0.25, making the fire significantly darker!
    vec3 finalRGB = activeFireRGB * brightnessMultiplier;

    // 7. Compute Masked Alpha Boundaries
    float finalAlpha = textSample.a * fireIntensity * vertexColor.a;

    fragColor = vec4(finalRGB, finalAlpha) * ColorModulator;

    if (fragColor.a < 0.05) {
        discard;
    }
}