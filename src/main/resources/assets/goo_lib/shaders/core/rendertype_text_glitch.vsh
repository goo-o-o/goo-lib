#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;

out vec4 vertexColor;
out vec2 texCoord;
out vec2 screenPos;
out float bandDisplacement;

float hash(float n) {
    return fract(sin(n) * 43758.5453123);
}

void main() {
    vec4 pos = vec4(Position, 1.0);
    vec4 mvPos = ModelViewMat * pos;

    // Fast, asynchronous step time to break up loop predictability
    float t1 = floor(GameTime * 24000.0 * 9.0);
    float t2 = floor(GameTime * 24000.0 * 16.5);

    // Slice Y positions into horizontal bands
    float band = floor(mvPos.y * 0.15);
    float bandSeed = hash(band + t1);

    // Random trigger for horizontal band shearing
    float isSheared = step(0.78, bandSeed);

    // Bidirectional displacement [-1.0, +1.0]
    float xShift = (hash(bandSeed * 13.0 + t2) - 0.5) * 2.0 * 10.0 * isSheared;
    float yShift = (hash(bandSeed * 29.0 + t1) - 0.5) * 2.0 * 3.0 * step(0.90, bandSeed);

    mvPos.x += xShift;
    mvPos.y += yShift;

    // Apply minor horizontal skewing to sheared bands
    mvPos.x += (UV0.y - 0.5) * xShift * 0.4;

    gl_Position = ProjMat * mvPos;

    vertexColor = Color;
    texCoord = UV0;
    screenPos = gl_Position.xy / gl_Position.w;
    bandDisplacement = xShift;
}