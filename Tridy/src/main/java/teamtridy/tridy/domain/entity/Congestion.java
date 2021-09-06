package teamtridy.tridy.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Congestion extends BaseTimeEntity implements Serializable { //혼잡도 캐시용도 테이블. 추후 레디스로 변경 가능성 있음.
    @Id
    @OneToOne
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private Integer level;
}
