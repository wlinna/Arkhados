#define M_PI 3.1415926535897932384626433832795
#define M_2PI (2.0 * M_PI)

varying vec3 pos;

uniform float g_Time;

void main(void)
{
   float revolutionsPerSecond = 3.0;
   float angleSpeed = revolutionsPerSecond * M_2PI;

   float shineAngle = mod(angleSpeed * g_Time, M_2PI);
   vec2 shinePosition = vec2(cos(shineAngle), sin(shineAngle));
  
   vec2 displacement = shinePosition - pos.xz;

   float opacity = 1.0 - length(displacement);

    float radius = length(pos.xz);
    float radiusScaler = -14.0*radius*radius - 2.0*(-14.0)*0.8*radius - 8.0;
   
   gl_FragColor = vec4(vec3(radiusScaler, 0.0, 0.0), opacity);
}