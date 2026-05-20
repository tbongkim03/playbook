package playbook.encore.back.terms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import playbook.encore.back.common.response.Response;
import playbook.encore.back.common.response.ResponseHandler;
import playbook.encore.back.terms.dto.TermsRequestDto;
import playbook.encore.back.terms.service.TermsService;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @GetMapping("/{type}")
    public ResponseEntity<Response> getTerms(@PathVariable String type) {
        try {
            return ResponseEntity.ok(ResponseHandler.success(termsService.getTerms(type)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseHandler.noData());
        }
    }

    @PutMapping("/{type}")
    public ResponseEntity<Response> updateTerms(
            HttpServletRequest request,
            @PathVariable String type,
            @RequestBody @Valid TermsRequestDto dto
    ) {
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equalsIgnoreCase((String) session.getAttribute("role"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ResponseHandler.notAuthorized());
        }
        try {
            return ResponseEntity.ok(ResponseHandler.success(termsService.updateTerms(type, dto)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseHandler.invalidParam(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseHandler.unknownError());
        }
    }
}
