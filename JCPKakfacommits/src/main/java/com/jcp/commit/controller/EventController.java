package com.jcp.commit.controller;

import com.jcp.commit.dto.audit.CommitsResponseDto;
import com.jcp.commit.dto.audit.CommitsResponseKeyDto;
import com.jcp.commit.dto.request.IdealNodeRequestDto;
import com.jcp.commit.dto.request.StartEndDateRequestDto;
import com.jcp.commit.dto.response.IdealNodeResponseDto;
import com.jcp.commit.hub.EventReceiver;
import com.jcp.commit.kafka.service.KafkaEventProducer;
import com.jcp.commit.service.IdealNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping(value = EventController.ENDPOINT)
public class EventController {

    static final String ENDPOINT = "/event";

    @Autowired
    private KafkaEventProducer kafkaEventProducer;

    @Autowired
    private EventReceiver eventReceiver;

    @Autowired
    private IdealNodeService idealNodeService;

    @PostMapping("/hub/post-ideal-node-to-kafka")
    public ResponseEntity<String> produceKafkaMessage(@Valid @RequestBody CommitsResponseDto commitsResponseDto) {

        kafkaEventProducer.send(CommitsResponseKeyDto.builder().build(), commitsResponseDto);

        return ResponseEntity
                .ok()
                .body("Success");

    }

    @GetMapping("/hub/health")
    public ResponseEntity<String> checkHealth() {

        return ResponseEntity
                .ok()
                .body("Success");

    }

    @GetMapping("/hub/read/messages")
    public ResponseEntity<String> readMessage() throws IOException {

        eventReceiver.receiveMessage();
        return ResponseEntity
                .ok()
                .body("Success");

    }

    @GetMapping("/hub/read-historic-data")
    public ResponseEntity<String> readHistoricData() throws IOException {

        final long start = System.currentTimeMillis();

       idealNodeService.readHistoricData("ram.csv");

        log.info("Read file: Time taken : {} ms", System.currentTimeMillis() - start);

        return ResponseEntity
                .ok()
                .body("Success");

    }

    @PostMapping("/hub/process-historic-data")
    public ResponseEntity<String> processHistoricData(@Valid @RequestBody StartEndDateRequestDto startEndDateRequestDto)
            throws IOException {

        final long start = System.currentTimeMillis();

        log.info("Read file: Time taken : {} ms", System.currentTimeMillis() - start);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(startEndDateRequestDto.getStartTime(), formatter);
        LocalDateTime endTime = LocalDateTime.parse(startEndDateRequestDto.getEndTime(), formatter);

        idealNodeService.processHistoricData(startTime, endTime);
        return ResponseEntity
                .ok()
                .body("Success");

    }

    @PostMapping("/hub/ideal-node")
    public ResponseEntity<IdealNodeResponseDto> getIdealNode(@Valid @RequestBody IdealNodeRequestDto idealNodeRequestDto) {

        final long start = System.currentTimeMillis();

        IdealNodeResponseDto idealNode = idealNodeService.getIdealNode(idealNodeRequestDto);

        log.info("Ideal node : Time taken : {} ms", System.currentTimeMillis() - start);

        return ResponseEntity
                .ok()
                .body(idealNode);

    }
}
