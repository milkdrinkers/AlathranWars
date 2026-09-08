package io.github.alathra.alathranwars.data.type;

import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class InstantDataField extends CustomDataField<Instant> {
    public InstantDataField(String key, Instant value, String label) {
        super(key, value, label);
    }

    public InstantDataField(String key, Instant value) {
        super(key, value);
    }

    public InstantDataField(String key) {
        super(key, Instant.now());
    }

    @NotNull
    @Override
    public String getTypeID() {
        return "towny_instantdf";
    }

    @Override
    public void setValueFromString(String strValue) {
        setValue(Instant.ofEpochMilli(Long.parseLong(strValue)));
    }

    @Nullable
    @Override
    protected String serializeValueToString() {
        return String.valueOf(getValue().toEpochMilli());
    }

    @Override
    protected String displayFormattedValue() {
        return Colors.LightGreen + getValue();
    }

    @NotNull
    @Override
    public CustomDataField<Instant> clone() {
        return new InstantDataField(getKey(), getValue(), getLabel());
    }
}
