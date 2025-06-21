package com.application.Model;

import java.util.List;

public class InlineKeyboard {
    List<Button> keys;
    String text;

    public InlineKeyboard(List<Button> list, String text) {
        this.keys = list;
        this.text = text;
    }

    public String toJson() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"inline_keyboard\":[");

        for (int i = 0; i < keys.size(); i++) {
            if (i % 2 == 0) {
                if (i != 0) builder.append("],");
                builder.append("[");
            } else {
                builder.append(",");
            }
            Button btn = keys.get(i);
            StringBuilder append = builder.append(String.format("{\"text\":\"%s\",\"callback_data\":\"%s\"}", btn.getText(), btn.getId()));
        }

        builder.append("]]}}");
        return builder.toString();
    }
}
