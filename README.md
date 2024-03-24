# CanyonRacer

Game inspired by Wipeout and Star Wars pod racer.

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3.
- `teavm`: Experimental web platform using TeaVM and WebGL.




12/03/2024:
- Last week a pull request was merged into LibGDX 1.12.2-SNAPSHOT to support anti-aliased frame buffers. This allows to use antialiasing in combination
 with post-processing shaders (e.g. vignette effect). This is only possible on desktop. The teavm version, uses FBO without AA.

17/03/2024:
 - Error messages on Intel GPU
        [LWJGL] GLFW_PLATFORM_ERROR error
        Description : WGL: Failed to make context current: The handle is invalid.
        Stacktrace  :
        org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(GLFW.java:4894)
        com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window.makeCurrent(Lwjgl3Window.java:431)
        com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application.loop(Lwjgl3Application.java:189)

    - is related to enabling GL31 even when we are not using it (required for multi sample fbo: Framebuffer multisample requires GLES 3.1+)
    -         configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL31, 4,3);
    - also occurs for GL30 but not for GL20
    - Assumed to be intel driver bug, annoying but doesn't break anything.



Breaking error on teavm version:
- java.lang.IllegalArgumentException: Comparison method violates its general contract!
- It seems to be within sceneManager, it does not occur if there are zero scenes in sceneManager

to do:
- DONE: collision with rocks
- camera avoidance of obstacles
- camera orbit control
- way points
- DONE: race time
- ai racers




Music:
fight.ogg via OpenGameArt.ogg, If you use this please (totally optional) credit to Ville Nousiainen and/or provide a link to http://soundcloud.com/mutkanto

Textures
https://freepbr.com/materials/eroded-smoothed-rockface/

Rust texture by Radetzki Vladimir https://www.behance.net/Radetzki_Vladimir

Rock surfaces in Blender tutorial
https://www.youtube.com/watch?v=q9GbbZKBwNg
