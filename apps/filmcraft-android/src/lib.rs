//! Native Android entry point for the original FilmCraft egui editor.
//! Android input and lifecycle integration is provided by eframe/winit.
#[cfg(target_os = "android")]
#[unsafe(no_mangle)]
pub fn android_main(app: android_activity::AndroidApp) {
    let private_files = app.config().internal_data_path.clone();
    let options = eframe::NativeOptions {
        android_app: Some(app),
        renderer: eframe::Renderer::Wgpu,
        ..Default::default()
    };
    let _ = eframe::run_native(
        "FilmCraft",
        options,
        Box::new(|cc| {
            // Increase physical hit targets for fingers without magnifying the
            // entire multi-panel desktop layout beyond the phone screen.
            cc.egui_ctx.set_zoom_factor(1.2);
            let mut editor = filmcraft_ui_egui::FilmcraftApp::new(filmcraft_engine::Session::default());
            if let Some(root) = private_files.as_ref() {
                let projects = root.join("projects");
                if std::fs::create_dir_all(&projects).is_ok() {
                    let save_dir = projects.clone();
                    editor.hooks.pick_save = Some(Box::new(move |name| {
                        let safe: String = name.chars().map(|c| {
                            if c.is_ascii_alphanumeric() || matches!(c, '.' | '-' | '_' | ' ') { c } else { '_' }
                        }).collect();
                        Some(save_dir.join(safe).to_string_lossy().into_owned())
                    }));
                    editor.hooks.pick_save_as = Some(Box::new(move |_, _, name| {
                        let safe: String = name.chars().map(|c| {
                            if c.is_ascii_alphanumeric() || matches!(c, '.' | '-' | '_' | ' ') { c } else { '_' }
                        }).collect();
                        Some(projects.join(safe).to_string_lossy().into_owned())
                    }));
                }
            }
            // Avoid Mali Vulkan driver crash in FilmCraft GPU compositor during startup.
            // egui still renders through eframe wgpu; only preview compositing is disabled.
            Ok(Box::new(editor))
        }),
    );
}
