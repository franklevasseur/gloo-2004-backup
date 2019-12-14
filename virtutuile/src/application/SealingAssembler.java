package application;

import Domain.SealsInfo;

public class SealingAssembler {

    public SealsInfoDto toDto(SealsInfo sealsInfo) {
        if (sealsInfo == null) {
            return null;
        }

        SealsInfoDto dto = new SealsInfoDto();
        dto.sealWidth = sealsInfo.getWidth();
        dto.color = sealsInfo.getColor();
        return dto;
    }

    public SealsInfo fromDto(SealsInfoDto sDto) {
        if (sDto == null) {
            return null;
        }

        double width = sDto.sealWidth;
        SealsInfo sealsInfo = new SealsInfo(width, sDto.color);
        return  sealsInfo;
    }
}
