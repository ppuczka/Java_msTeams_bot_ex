package com.ppuczka.teamsbot.modules;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TaskModuleUIConstants {


    public static final UISettings CUSTOMFORM = new UISettings(
            510,
            450,
            "Custom Form",
            TaskModuleIds.CUSTOMFORM.toString(),
            "Custom Form"
    );

    public static final UISettings ADAPTIVECARD = new UISettings(
            400,
            200,
            "Adaptive Card: Inputs",
            TaskModuleIds.ADAPTIVECARD.toString(),
            "Adaptive Card"
    );
}
}
