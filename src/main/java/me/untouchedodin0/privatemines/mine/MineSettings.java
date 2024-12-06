package me.untouchedodin0.privatemines.mine;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MineSettings {
    private boolean open;
    private double tax;
}
