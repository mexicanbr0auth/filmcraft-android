//! Native Android entry point for the original FilmCraft egui editor.
//! Android input and lifecycle integration is provided by eframe/winit.
#[cfg(target_os = "android")]
#[unsafe(no_mangle)]
pub fn android_main(app: android_activity::AndroidApp) {
    android_activity::AndroidApp::set_text_input_state(&app, android_activity::TextInputState::default());
    let options = eframe::NativeOptions {
        android_app: Some(app),
        renderer: eframe::Renderer::Wgpu,
        ..Default::default()
    };
    let _ = eframe::run_native(
        "FilmCraft",
        options,
        Box::new(|cc| {
            let mut editor = filmcraft_ui_egui::FilmcraftApp::new(filmcraft_engine::Session::default());
            if let Some(render_state) = cc.wgpu_render_state.clone() {
                editor.set_wgpu(render_state);
            }
            Ok(Box::new(editor))
        }),
    );
}
