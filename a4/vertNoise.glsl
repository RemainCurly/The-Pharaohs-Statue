#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
out vec3 varyingNormal;
out vec3 originalPosition;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec4 shadow_coord;
out float wantLights;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform float choice;

layout (binding=0) uniform sampler3D s;    //Usage of 3D Texture noise
layout (binding=1) uniform sampler2DShadow shadowTex;

void main(void)
{	
	varyingNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	originalPosition = position;
	varyingVertPos = (mv_matrix * vec4(position,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	
	shadow_coord = shadowMVP * vec4(position, 1.0);
	
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
	wantLights = choice;
}