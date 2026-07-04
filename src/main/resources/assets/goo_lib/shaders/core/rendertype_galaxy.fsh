#version 150

// Star Nest Parameters
#define ITERATIONS    14
#define FORMUPARAM    0.53
#define VOLSTEPS      16
#define STEPSIZE      0.1

#define ZOOM          0.400 // Adjusted zoom for a gorgeous screen space vista
#define TILE          0.850
#define SPEED         0.020 // Smooth traveling speed

#define BRIGHTNESS    0.0015
#define DARKMATTER    0.300
#define DISTFADING    0.730
#define SATURATION    0.850

uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
out vec4 fragColor;

void main() {
    // 2. Dynamic Screen-Space Coordinate Reconstruction
    float screenWidth = 1.0 / length(dFdx(gl_FragCoord.xy));
    float screenHeight = 1.0 / length(dFdy(gl_FragCoord.xy));
    vec2 resolution = vec2(screenWidth, screenHeight);

    // Replicate Shadertoy's exact screen layout:
    vec2 uv = gl_FragCoord.xy / resolution.xy - 0.5;
    uv.y *= resolution.y / resolution.x; // Perfect aspect ratio squaring

    vec3 dir = vec3(uv * ZOOM, 1.0);

    // Scale system time
    float time = (GameTime * 100.0) * SPEED + 0.25;

    // 3. Camera Movement Flight Path
    vec3 from = vec3(1.0, 0.5, 0.5);
    from += vec3(time * 2.0, time, -2.0);

    // 4. Volumetric Fractal Raymarching Loop
    float s = 0.1;
    float fade = 1.0;
    vec3 v = vec3(0.0);

    for (int r = 0; r < VOLSTEPS; r++) {
        vec3 p = from + s * dir * 0.5;
        p = abs(vec3(TILE) - mod(p, vec3(TILE * 2.0))); // Tiling space folding

        float pa = 0.0;
        float a = 0.0;

        for (int i = 0; i < ITERATIONS; i++) {
            p = abs(p) / dot(p, p) - FORMUPARAM;
            // The Kaliset Magic Formula
            a += abs(length(p) - pa);
            pa = length(p);
        }

        float dm = max(0.0, DARKMATTER - a * a * 0.001);
        a *= a * a;

        if (r > 6) {
            fade *= 1.0 - dm;
        }

        v += fade;
        v += vec3(s, s * s, s * s * s * s) * a * BRIGHTNESS * fade;
        fade *= DISTFADING;
        s += STEPSIZE;
    }

    v = mix(vec3(length(v)), v, SATURATION);
    vec3 cosmicRGB = v * 0.01;

    // 5. Blending & Color Modulators
    // FIX: Removed texSample.a and replaced it with solid 1.0 base transparency
    vec4 finalColor = vec4(cosmicRGB, 1.0 * ColorModulator.a) * vec4(vertexColor.rgb, 1.0);
    finalColor.rgb *= ColorModulator.rgb;

    // Multiply by vertexColor.a to seamlessly hide/show the overlay with Java code
    fragColor = finalColor * vertexColor.a;
}