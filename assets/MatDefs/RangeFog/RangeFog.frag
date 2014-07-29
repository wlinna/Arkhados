varying vec4 characterPos;
varying vec4 vertexWorldPos; 

void main() {
    float d = sqrt(
                pow(vertexWorldPos.x - characterPos.x, 2.0)
              + pow(vertexWorldPos.z - characterPos.z, 2.0)
                 );

    float alpha = clamp(0.4 * d / (120 - 80) - 0.4 * 80 / (120 - 80), 0, 0.4);
    gl_FragColor = vec4(0.0, 0.0, 0.0, alpha);   
}