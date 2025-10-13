package abeshutt.staracademy.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class TextUtils {

    public static MutableText replace(Text text, String target, Text replacement) {
        List<Text> parts = new ArrayList<>();

        text.getContent().visit((string) -> {
            String[] raw = string.split(Pattern.quote(target));

            MutableText copy = Text.empty();
            copy.setStyle(text.getStyle());

            for(int i = 0; i < raw.length; i++) {
                copy.getSiblings().add(Text.literal(raw[i]));

                if(i != raw.length - 1) {
                    copy.getSiblings().add(replacement.copy());
                }
            }

            parts.add(copy);
            return Optional.empty();
        });

        MutableText copy = Text.empty();
        copy.setStyle(text.getStyle());
        copy.getSiblings().clear();

        for(Text sibling : text.getSiblings()) {
           copy.getSiblings().add(replace(sibling, target, replacement));
        }

        parts.add(copy);

        MutableText result = Text.empty();

        for(Text part : parts) {
           result.getSiblings().add(part);
        }

        return result;
    }

}
