#version 430

layout(location=0) in vec3 position;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
out vec4 axesColor;

void main(void)
{
	gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
	
	//Sets RGB value of lines based on gl_VertexID
	if(gl_VertexID < 2)
		axesColor = vec4(1.0, 0.0, 0.0, 1.0);
	else if(gl_VertexID >= 2 && gl_VertexID < 4)
		axesColor = vec4(0.0, 1.0, 0.0, 1.0);
	else
		axesColor = vec4(0.0, 0.0, 1.0, 1.0);
}