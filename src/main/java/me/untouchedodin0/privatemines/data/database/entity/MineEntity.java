package me.untouchedodin0.privatemines.data.database.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

import java.util.UUID;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MineEntity {
    @Id @GeneratedValue private UUID id;
    private UUID owner;
    private String mineTemplate;
    @Embedded
    private Location location;

}
