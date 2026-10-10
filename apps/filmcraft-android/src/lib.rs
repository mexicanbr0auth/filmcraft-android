//! Native Android entry point for the original FilmCraft egui editor.
//! Android input and lifecycle integration is provided by eframe/winit.
#[cfg(target_os = "android")]
#[unsafe(no_mangle)]
pub fn android_main(app: android_activity::AndroidApp) {
    let options = eframe::NativeOptions {
        android_app: Some(app),
        renderer: eframe::Renderer::Wgpu,
        ..Default::default()
    };
    let _ = eframe::run_native(
        "FilmCraft",
        options,
        Box::new(|_cc| {
            let editor = filmcraft_ui_egui::FilmcraftApp::new(filmcraft_engine::Session::default());
            // Avoid Mali Vulkan driver crash in FilmCraft GPU compositor during startup.
            // egui still renders through eframe wgpu; only preview compositing is disabled.
            Ok(Box::new(editor))
        }),
    );
}
