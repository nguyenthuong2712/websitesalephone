package org.example.websitesalephone.service.dyanmic.impl;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.dto.dynamic.DynamicRequest;
import org.example.websitesalephone.dto.dynamic.DynamicResponse;
import org.example.websitesalephone.dto.dynamic.DynamicSearch;
import org.example.websitesalephone.entity.*;
import org.example.websitesalephone.enums.DynamicEnum;
import org.example.websitesalephone.repository.*;
import org.example.websitesalephone.service.dyanmic.DynamicAttributeProductService;
import org.example.websitesalephone.comon.CommonResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DynamicAttributeProductServiceImpl implements DynamicAttributeProductService {

    private final ColorRepository colorRepository;

    private final OriginRepository originRepository;

    private final ScreenRepository screenRepository;

    private final OperatingSystemRepository operatingSystemRepository;

    private final CameraRepository cameraRepository;

    private final BatteryRepository batteryRepository;

    private final RamRepository ramRepository;

    private final StorageRepository storageRepository;

    private final CpuRepository cpuRepository;

    @Override
    public CommonResponse getALl(DynamicSearch dynamicSearch) {
        List<DynamicResponse> dynamics;

        DynamicEnum type = Arrays.stream(DynamicEnum.values())
                .filter(e -> e.getValue().equalsIgnoreCase(dynamicSearch.getDynamicEnum()))
                .findFirst()
                .orElse(null);

        switch (type) {
            case COLOR -> dynamics = colorRepository.findAll()
                    .stream()
                    .map(c -> new DynamicResponse(c.getId(), c.getName()))
                    .toList();

            case ORIGIN -> dynamics = originRepository.findAll()
                    .stream()
                    .map(o -> new DynamicResponse(o.getId(), o.getName()))
                    .toList();

            case SCREEN -> dynamics = screenRepository.findAll()
                    .stream()
                    .map(s -> new DynamicResponse(s.getId(), s.getName()))
                    .toList();

            case OPS -> dynamics = operatingSystemRepository.findAll()
                    .stream()
                    .map(os -> new DynamicResponse(os.getId(), os.getName()))
                    .toList();

            case RAM -> dynamics = ramRepository.findAll()
                    .stream()
                    .map(r -> new DynamicResponse(r.getId(), r.getName()))
                    .toList();

            case STORAGE -> dynamics = storageRepository.findAll()
                    .stream()
                    .map(st -> new DynamicResponse(st.getId(), st.getName()))
                    .toList();

            case BATTERY -> dynamics = batteryRepository.findAll()
                    .stream()
                    .map(b -> new DynamicResponse(b.getId(), b.getName()))
                    .toList();

            case SPU -> dynamics = cpuRepository.findAll()
                    .stream()
                    .map(cpu -> new DynamicResponse(cpu.getId(), cpu.getName()))
                    .toList();

            case CAMERA -> dynamics = cameraRepository.findAll()
                    .stream()
                    .map(cam -> new DynamicResponse(cam.getId(), cam.getName()))
                    .toList();

            default -> {
                return CommonResponse.builder().message("Không tìm thấy loại cần lấy").build();
            }
        }

        return CommonResponse.builder().data(dynamics).build();
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse created(DynamicRequest dynamicRequest) {

        DynamicEnum type = Arrays.stream(DynamicEnum.values())
                .filter(e -> e.getValue().equalsIgnoreCase(dynamicRequest.getDynamicName()))
                .findFirst()
                .orElse(null);

        Object savedEntity;

        switch (type) {
            case COLOR -> {
                Color c = new Color();
                c.setId(UUID.randomUUID().toString());
                c.setName(dynamicRequest.getName());
                savedEntity = colorRepository.save(c);
            }

            case ORIGIN -> {
                Origin o = new Origin();
                o.setId(UUID.randomUUID().toString());
                o.setName(dynamicRequest.getName());
                savedEntity = originRepository.save(o);
            }

            case SCREEN -> {
                Screen s = new Screen();
                s.setId(UUID.randomUUID().toString());
                s.setName(dynamicRequest.getName());
                savedEntity = screenRepository.save(s);
            }

            case OPS -> {
                OperatingSystem os = new OperatingSystem();
                os.setId(UUID.randomUUID().toString());
                os.setName(dynamicRequest.getName());
                savedEntity = operatingSystemRepository.save(os);
            }

            case RAM -> {
                Ram r = new Ram();
                r.setId(UUID.randomUUID().toString());
                r.setName(dynamicRequest.getName());
                savedEntity = ramRepository.save(r);
            }

            case STORAGE -> {
                Storage st = new Storage();
                st.setId(UUID.randomUUID().toString());
                st.setName(dynamicRequest.getName());
                savedEntity = storageRepository.save(st);
            }

            case BATTERY -> {
                Battery b = new Battery();
                b.setId(UUID.randomUUID().toString());
                b.setName(dynamicRequest.getName());
                savedEntity = batteryRepository.save(b);
            }

            case SPU -> {
                Cpu cpu = new Cpu();
                cpu.setId(UUID.randomUUID().toString());
                cpu.setName(dynamicRequest.getName());
                savedEntity = cpuRepository.save(cpu);
            }

            case CAMERA -> {
                Camera cam = new Camera();
                cam.setId(UUID.randomUUID().toString());
                cam.setName(dynamicRequest.getName());
                savedEntity = cameraRepository.save(cam);
            }

            default -> {
                return CommonResponse.builder().message("Không tìm thấy loại cần lấy").build();
            }
        }

        return CommonResponse.builder().data(savedEntity).build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CommonResponse updated(DynamicRequest dynamicRequest) {

        DynamicEnum type = Arrays.stream(DynamicEnum.values())
                .filter(e -> e.getValue().equalsIgnoreCase(dynamicRequest.getDynamicName()))
                .findFirst()
                .orElse(null);

        Object updatedEntity;

        switch (type) {
            case COLOR -> {
                var opt = colorRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Color c = opt.get();
                c.setName(dynamicRequest.getName());
                updatedEntity = colorRepository.save(c);
            }

            case ORIGIN -> {
                var opt = originRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Origin o = opt.get();
                o.setName(dynamicRequest.getName());
                updatedEntity = originRepository.save(o);
            }

            case SCREEN -> {
                var opt = screenRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Screen s = opt.get();
                s.setName(dynamicRequest.getName());
                updatedEntity = screenRepository.save(s);
            }

            case OPS -> {
                var opt = operatingSystemRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                OperatingSystem os = opt.get();
                os.setName(dynamicRequest.getName());
                updatedEntity = operatingSystemRepository.save(os);
            }

            case RAM -> {
                var opt = ramRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Ram r = opt.get();
                r.setName(dynamicRequest.getName());
                updatedEntity = ramRepository.save(r);
            }

            case STORAGE -> {
                var opt = storageRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Storage st = opt.get();
                st.setName(dynamicRequest.getName());
                updatedEntity = storageRepository.save(st);
            }

            case BATTERY -> {
                var opt = batteryRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Battery b = opt.get();
                b.setName(dynamicRequest.getName());
                updatedEntity = batteryRepository.save(b);
            }

            case SPU -> {
                var opt = cpuRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Cpu cpu = opt.get();
                cpu.setName(dynamicRequest.getName());
                updatedEntity = cpuRepository.save(cpu);
            }

            case CAMERA -> {
                var opt = cameraRepository.findById(dynamicRequest.getId());
                if (opt.isEmpty()) return CommonResponse.builder().message("Không tìm thấy ID").build();
                Camera cam = opt.get();
                cam.setName(dynamicRequest.getName());
                updatedEntity = cameraRepository.save(cam);
            }

            default -> {
                return CommonResponse.builder().message("Không tìm thấy loại cần cập nhật").build();
            }
        }

        return CommonResponse.builder().data(updatedEntity).build();
    }


    @Override
    public CommonResponse deleted(DynamicRequest dynamicRequest) {

        DynamicEnum type = Arrays.stream(DynamicEnum.values())
                .filter(e -> e.getValue().equalsIgnoreCase(dynamicRequest.getDynamicName()))
                .findFirst()
                .orElse(null);

        switch (type) {
            case COLOR -> colorRepository.deleteById(dynamicRequest.getId());
            case ORIGIN -> originRepository.deleteById(dynamicRequest.getId());
            case SCREEN -> screenRepository.deleteById(dynamicRequest.getId());
            case OPS -> operatingSystemRepository.deleteById(dynamicRequest.getId());
            case RAM -> ramRepository.deleteById(dynamicRequest.getId());
            case STORAGE -> storageRepository.deleteById(dynamicRequest.getId());
            case BATTERY -> batteryRepository.deleteById(dynamicRequest.getId());
            case SPU -> cpuRepository.deleteById(dynamicRequest.getId());
            case CAMERA -> cameraRepository.deleteById(dynamicRequest.getId());

            default -> {
                return CommonResponse.builder().message("Không tìm thấy loại").build();
            }
        }

        return CommonResponse.builder().message("Xóa thành công").build();
    }

}
