#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;

out vec4 vertexColor;
out vec2 texCoord0;
out vec2 quadUv;    // normalized 0.0 to 1.0 local quad uv
out vec2 screenPos;

#define TIME (GameTime * 1000.0)

void main() {
    int corner = gl_VertexID % 4;
    if (corner == 0)      quadUv = vec2(0.0, 0.0);
    else if (corner == 1) quadUv = vec2(0.0, 1.0);
    else if (corner == 2) quadUv = vec2(1.0, 1.0);
    else                  quadUv = vec2(1.0, 0.0);

    vec3 pos = Position;

    // Heat shimmer: wobble intensity scales with (1.0 - quadUv.y), stronger at character tops
    float heatFactor = (1.0 - quadUv.y);
    float heatShimmerX = sin(TIME * 4 + pos.y * 0.5) * 0.65 * heatFactor;
    float heatShimmerY = cos(TIME * 3 + pos.x * 0.5) * 0.35 * heatFactor;

    pos.x += heatShimmerX;
    pos.y += heatShimmerY;

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    screenPos = gl_Position.xy / gl_Position.w;
    vertexColor = Color;
    texCoord0 = UV0;
}