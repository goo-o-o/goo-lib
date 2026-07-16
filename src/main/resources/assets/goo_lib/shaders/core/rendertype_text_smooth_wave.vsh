#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;
uniform float Amplitude;
uniform float Speed;
uniform float WaveFrequency;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 screenPos;

#define SCROLL_SPEED    (GameTime * 1000.0 * Speed)

void main() {
    vec4 localPos = vec4(Position, 1.0);

    float wavePhase = SCROLL_SPEED + (localPos.x * WaveFrequency);

    localPos.y += sin(wavePhase) * Amplitude;

    gl_Position = ProjMat * ModelViewMat * localPos;

    vertexColor = Color;
    texCoord0 = UV0;
    screenPos = Position.xy;


}