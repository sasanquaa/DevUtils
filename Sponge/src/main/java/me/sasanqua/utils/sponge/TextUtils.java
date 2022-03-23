package me.sasanqua.utils.sponge;

import com.google.common.base.Preconditions;
import me.sasanqua.utils.common.Builder;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

	public static final TextTemplateConverter DEFAULT_CONVERTER = converterBuilder().build();

	public static Text deserialize(String str) {
		return TextSerializers.FORMATTING_CODE.deserialize(str);
	}

	public static String serialize(Text text) {
		return TextSerializers.FORMATTING_CODE.serialize(text);
	}

	public static Text deserializeLegacy(String str) {
		return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(str);
	}

	public static String serializeLegacy(Text text) {
		return TextSerializers.LEGACY_FORMATTING_CODE.serialize(text);
	}

	public static TextTemplateConverterBuilder converterBuilder() {
		return new TextTemplateConverterBuilder();
	}

	public static class TextTemplateConverter {

		private final String openArg;
		private final String closeArg;
		private final Pattern pattern;

		TextTemplateConverter(TextTemplateConverterBuilder builder) {
			this.openArg = builder.openArg;
			this.closeArg = builder.closeArg;
			this.pattern = builder.pattern;
		}

		public TextTemplate convert(Text text) {
			List<TextRepresentable> parts = new ArrayList<>();
			String str = serialize(text);
			Matcher matcher = pattern.matcher(str);
			int i = 0;
			while (matcher.find()) {
				parts.add(deserialize(str.substring(i, matcher.start())));
				parts.add(TextTemplate.arg(str.substring(matcher.start() + 1, matcher.end() - 1)).build());
				i = matcher.end();
			}
			if (i < str.length()) {
				parts.add(deserialize(str.substring(i)));
			}
			return TextTemplate.of(openArg, closeArg, parts.toArray());
		}

	}

	public static class TextTemplateConverterBuilder implements Builder<TextTemplateConverter> {

		private String openArg = "%";
		private String closeArg = "%";
		private Pattern pattern = Pattern.compile("%([^%\\s]+)%", Pattern.CASE_INSENSITIVE);

		public TextTemplateConverterBuilder openArg(String openArg) {
			this.openArg = openArg;
			return this;
		}

		public TextTemplateConverterBuilder closeArg(String closeArg) {
			this.closeArg = closeArg;
			return this;
		}

		public TextTemplateConverterBuilder pattern(Pattern pattern) {
			this.pattern = pattern;
			return this;
		}

		@Override
		public TextTemplateConverter build() {
			Preconditions.checkNotNull(openArg, "Open argument cannot be null");
			Preconditions.checkNotNull(closeArg, "Close argument cannot be null");
			Preconditions.checkNotNull(pattern, "Pattern cannot be null");
			return new TextTemplateConverter(this);
		}

	}

}
