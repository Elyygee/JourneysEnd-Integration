package abeshutt.staracademy.data.biome;

import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.adapter.ISimpleAdapter;
import abeshutt.staracademy.data.adapter.array.ArrayAdapter;
import abeshutt.staracademy.data.bit.BitBuffer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

@FunctionalInterface
public interface BiomePredicate {

	BiomePredicate FALSE = (biome) -> false;
	BiomePredicate TRUE = (biome) -> true;

	boolean test(RegistryEntry<Biome> biome);

	default boolean test(Biome biome, DynamicRegistryManager registries) {
		return this.test(registries.get(RegistryKeys.BIOME).getEntry(biome));
	}

	static Optional<BiomePredicate> of(String string, boolean logErrors) {
		if(string.isEmpty()) {
			return Optional.of(TRUE);
		}

		return (switch(string.charAt(0)) {
			case '#' -> PartialBiomeTag.parse(string, logErrors);
			case '@' -> PartialBiomeGroup.parse(string, logErrors);
			default -> PartialBiome.parse(string, logErrors);
		}).map(o -> (BiomePredicate)o);
	}

	class Adapter implements ISimpleAdapter<BiomePredicate, NbtElement, JsonElement> {
		private static ArrayAdapter<BiomePredicate> LIST = Adapters.ofArray(BiomePredicate[]::new, new Adapter());

		@Override
		public void writeBits(BiomePredicate value, BitBuffer buffer) {
			buffer.writeBoolean(value == null);

			if(value != null) {
				if(value instanceof OrBiomePredicate or) {
					buffer.writeBoolean(true);
					LIST.writeBits(or.getChildren(), buffer);
				} else {
					buffer.writeBoolean(false);
					Adapters.UTF_8.writeBits(value.toString(), buffer);
				}
			}
		}

		@Override
		public final Optional<BiomePredicate> readBits(BitBuffer buffer) {
			if(buffer.readBoolean()) {
				return Optional.empty();
			}

			if(buffer.readBoolean()) {
				return LIST.readBits(buffer).map(OrBiomePredicate::new);
			}

			return Adapters.UTF_8.readBits(buffer).map(string -> of(string, true).orElse(FALSE));
		}

		@Override
		public Optional<NbtElement> writeNbt(BiomePredicate value) {
			if(value == null) {
				return Optional.empty();
			} else if(value instanceof OrBiomePredicate or) {
				return LIST.writeNbt(or.getChildren());
			}

			return Optional.of(NbtString.of(value.toString()));
		}

		@Override
		public Optional<BiomePredicate> readNbt(NbtElement nbt) {
			if(nbt == null) {
				return Optional.empty();
			}

			if(nbt instanceof NbtList list) {
				return LIST.readNbt(list).map(OrBiomePredicate::new);
			} else if(nbt instanceof NbtString string) {
				return Optional.of(of(string.asString(), true).orElse(FALSE));
			}

			return Optional.empty();
		}

		@Override
		public Optional<JsonElement> writeJson(BiomePredicate value) {
			if(value == null) {
				return Optional.empty();
			} else if(value instanceof OrBiomePredicate or) {
				return LIST.writeJson(or.getChildren());
			}

			return Optional.of(new JsonPrimitive(value.toString()));
		}

		@Override
		public Optional<BiomePredicate> readJson(JsonElement json) {
			if(json == null) {
				return Optional.empty();
			}

			if(json instanceof JsonArray array) {
				return LIST.readJson(array).map(OrBiomePredicate::new);
			} else if(json instanceof JsonPrimitive primitive && primitive.isString()) {
				return Optional.of(of(json.getAsString(), true).orElse(FALSE));
			}

			return Optional.empty();
		}
	}

}
