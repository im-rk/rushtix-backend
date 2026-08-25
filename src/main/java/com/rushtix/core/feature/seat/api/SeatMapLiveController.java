package com.rushtix.core.feature.seat.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*")
public class SeatMapLiveController {

    private final Map<String, List<SseEmitter>> emitters=new ConcurrentHashMap<>();

    @GetMapping(value = "/stream/{eventId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSeatUpdates(@PathVariable String eventId) {

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        this.emitters.computeIfAbsent(eventId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(eventId, emitter));
        emitter.onTimeout(() -> removeEmitter(eventId, emitter));
        emitter.onError((e) -> removeEmitter(eventId, emitter));

        return emitter;
    }

    public void broadcastUpdateToClients(String eventId, String jsonPayload)
    {
        List<SseEmitter> eventEmitters =emitters.get(eventId);
        if(eventEmitters!=null)
        {
            for(SseEmitter emitter:eventEmitters)
            {
                try {
                    emitter.send(SseEmitter.event()
                            .name("seat-update")
                            .data(jsonPayload));
                } catch (IOException e) {
                    emitter.complete();
                    removeEmitter(eventId, emitter);
                }
            }
        }
    }

    private void removeEmitter(String eventId, SseEmitter emitter)
    {
        List<SseEmitter> eventEmitters=emitters.get(eventId);
        if(eventEmitters!=null)
        {
            eventEmitters.remove(emitter);
            if(eventEmitters.isEmpty())
            {
                emitters.remove(eventId);
            }
        }
    }
}
