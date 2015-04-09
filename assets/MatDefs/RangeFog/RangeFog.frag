varying vec4 characterPos;
varying vec4 vertexWorldPos; 

void main() {
    float dist = sqrt(
                pow(vertexWorldPos.x - characterPos.x, 2.0)
              + pow(vertexWorldPos.z - characterPos.z, 2.0)
                 );

    /* 170 is max distance, 130 is where darkening starts */
    float alpha = clamp((dist - 130.0) * 0.4 / (170.0 - 130.0), 0.0, 0.4);

    if (alpha <= 0.02) {
       discard;
    }

    gl_FragColor = vec4(0.0, 0.0, 0.0, alpha);   
}