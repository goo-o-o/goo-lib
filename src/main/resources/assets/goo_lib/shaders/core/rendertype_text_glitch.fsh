#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord;
in vec2 screenPos;
in float bandDisplacement;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

// This code was generated with AI with human supervision and creative direction
void main() {
    // Multi-octave time stepping for varied glitch rates
    float timeSlow = floor(GameTime * 24000.0 * 5.0);
    float timeFast = floor(GameTime * 24000.0 * 13.0);

    // --- 1. Dynamic Box Cutouts (Random Sizes & Placements) ---
    // Scaled grid using prime offsets to prevent tiling repetition
    vec2 grid = floor(screenPos * vec2(17.0, 31.0));
    float boxNoise = hash(grid + vec2(timeSlow, timeFast));

    if (boxNoise > 0.88) {
        discard;
    }

    // --- 2. Bidirectional Text Duplicates / Ghosting ---
    vec2 ghostOffset = vec2(0.0);
    float ghostTrigger = hash(vec2(timeSlow, 7.3));

    if (ghostTrigger > 0.72) {
        // Randomly offset left OR right [-0.035, +0.035]
        float dirX = (hash(vec2(timeFast, 1.1)) - 0.5) * 2.0;
        float dirY = (hash(vec2(timeSlow, 4.4)) - 0.5) * 0.4; // subtle Y drift
        ghostOffset = vec2(dirX * 0.035, dirY * 0.01);
    }

    vec2 mainUV = texCoord + ghostOffset;

    // --- 3. Bidirectional Chromatic Aberration ---
    // Chromatic split flips direction randomly per glitch pulse
    float abDir = sign(hash(vec2(timeFast, 9.2)) - 0.5);
    float abIntensity = (0.0012 + abs(bandDisplacement) * 0.0003) * (1.0 + hash(vec2(timeSlow)) * 2.5);
    vec2 abOffset = vec2(abIntensity * abDir, 0.0);

    // Sample channels individually across font atlas
    float r = texture(Sampler0, mainUV + abOffset).a;
    float g = texture(Sampler0, mainUV).a;
    float b = texture(Sampler0, mainUV - abOffset).a;

    vec4 baseTex = texture(Sampler0, mainUV);
    if (baseTex.a < 0.01 && r < 0.01 && b < 0.01) {
        discard;
    }

    // Reconstruct RGB with chromatic separation
    vec3 chromaticColor = vec3(r, g, b) * vertexColor.rgb;
    float finalAlpha = max(g, max(r, b)) * vertexColor.a;

    // Occasional bright signal flash/inversion
    float flashNoise = hash(vec2(timeFast, screenPos.y));
    if (flashNoise > 0.97) {
        chromaticColor += vec3(0.4); // luminance boost
    }

    fragColor = vec4(chromaticColor, finalAlpha) * ColorModulator;
}