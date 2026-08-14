#version 150

uniform sampler2D DiffuseSampler;
uniform float PixelSize;
uniform vec2 OutSize;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 screenSize = OutSize;

    vec2 cellOrigin = floor(texCoord * screenSize / PixelSize) * PixelSize / screenSize;

    vec4 best = vec4(0.0);
    for (float sx = 0.0; sx < PixelSize; sx += 1.0) {
        for (float sy = 0.0; sy < PixelSize; sy += 1.0) {
            vec4 s = texture(DiffuseSampler, cellOrigin + vec2(sx, sy) / screenSize);
            if (s.a > best.a) best = s;
        }
    }

    if (best.a > 0.05) {
        fragColor = vec4(best.rgb, 1.0);
    } else {
        discard;
    }
}