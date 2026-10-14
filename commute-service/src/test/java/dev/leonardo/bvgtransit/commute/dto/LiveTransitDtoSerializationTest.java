package dev.leonardo.bvgtransit.commute.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

class LiveTransitDtoSerializationTest {

    @Test
    void testSerialization() {
        String redisJson =
"""
    {
      "tripId": "1|2566|10|86|9102026",
      "stop": {
        "type": "stop",
        "id": "900100003",
        "name": "S+U Alexanderplatz Bhf (Berlin)",
        "location": {
          "type": "location",
          "id": "900100003",
          "latitude": 52.521508,
          "longitude": 13.411267
        },
        "products": {
          "suburban": true,
          "subway": true,
          "tram": false,
          "bus": false,
          "ferry": false,
          "express": false,
          "regional": false
        }
      },
      "when": "2026-10-09T14:30:00+02:00",
      "plannedWhen": "2026-10-09T14:30:00+02:00",
      "delay": 0,
      "platform": "4",
      "plannedPlatform": "4",
      "prognosisType": "prognosed",
      "direction": "S Potsdam Hauptbahnhof",
      "provenance": null,
      "line": {
        "type": "line",
        "id": "de-vbb-11000000-s-bahn-s7-s7-oi",
        "fahrtNr": "22772",
        "name": "S7",
        "public": true,
        "adminCode": "DBS---",
        "productName": "S",
        "mode": "train",
        "product": "suburban",
        "operator": {
          "type": "operator",
          "id": "s-bahn-berlin-gmbh",
          "name": "S-Bahn Berlin GmbH"
        }
      },
      "remarks": [
        {
          "type": "hint",
          "code": "FK",
          "text": "Bicycle conveyance"
        }
      ],
      "origin": null,
      "destination": {
        "type": "stop",
        "id": "900230999",
        "name": "S Potsdam Hauptbahnhof",
        "location": {
          "type": "location",
          "id": "900230999",
          "latitude": 52.391443,
          "longitude": 13.06716
        },
        "products": {
          "suburban": true,
          "subway": false,
          "tram": true,
          "bus": true,
          "ferry": false,
          "express": true,
          "regional": true
        }
      },
      "currentTripPosition": {
        "type": "location",
        "latitude": 52.521544,
        "longitude": 13.411196
      }
    }
""";
        LiveTransitDto dto = LiveTransitDto.fromJson(redisJson);
        assertNotNull(dto);
        assertEquals(0, dto.delay());
        assertEquals("900230999", dto.destinationId());
        assertEquals("S Potsdam Hauptbahnhof", dto.direction());
        assertEquals(LocalTime.parse("14:30"), dto.when());
        assertEquals(LocalTime.parse("14:30"), dto.plannedWhen());
    }
}
