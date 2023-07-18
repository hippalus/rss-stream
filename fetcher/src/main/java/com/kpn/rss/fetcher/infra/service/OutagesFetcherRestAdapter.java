package com.kpn.rss.fetcher.infra.service;

import com.kpn.rss.fetcher.domain.model.Channel;
import com.kpn.rss.fetcher.domain.model.Item;
import com.kpn.rss.fetcher.domain.service.FetcherService;
import com.kpn.rss.fetcher.infra.service.client.OutagesRssClient;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rometools.utils.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Content;
import org.jdom2.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutagesFetcherRestAdapter implements FetcherService {

    private final OutagesRssClient outagesRssClient;

    @Override
    public Set<Item> fetchNewItems(final Set<Item> lastItems) {
        final ResponseEntity<byte[]> response = this.outagesRssClient.outages().block();

        final byte[] source = requireNonNull(requireNonNull(response).getBody());

        final InputStream inputStream = new ByteArrayInputStream(source);
        final Optional<SyndFeed> optionalSyndFeed = toFeed(inputStream);

        final Set<Item> items = optionalSyndFeed.map(this::getItems).orElseGet(Set::of);

        return items.stream()
                .filter(item -> !lastItems.contains(item))
                .collect(Collectors.toSet());
    }

    private Set<Item> getItems(final SyndFeed syndFeed) {
        final Channel channel = Channel.builder()
                .copyright(trim(syndFeed.getCopyright()))
                .title(trim(syndFeed.getTitle()))
                .description(trim(syndFeed.getDescription()))
                .link(trim(syndFeed.getLink()))
                .generator(trim(syndFeed.getGenerator()))
                .language(trim(syndFeed.getLanguage()))
                .build();

        return syndFeed.getEntries()
                .stream()
                .map(syndEntry -> this.toItem(channel, syndEntry))
                .collect(Collectors.toSet());
    }

    private Item toItem(final Channel channel, final SyndEntry syndEntry) {
        final String category = syndEntry.getCategories().stream()
                .findFirst()
                .map(SyndCategory::getName)
                .map(OutagesFetcherRestAdapter::trim)
                .orElse("");

        final List<Element> foreignMarkup = syndEntry.getForeignMarkup();

        final String postalCodes = extractFromForeignMarkup(foreignMarkup, "postalCodes");

        return Item.builder()
                .title(trim(syndEntry.getTitle()))
                .link(link(syndEntry))
                .category(category)
                .ticketNumber(extractFromForeignMarkup(foreignMarkup, "ticketNumber"))
                .expectedEndDate(extractFromForeignMarkup(foreignMarkup, "expectedEndDate"))
                .categoryJames(extractFromForeignMarkup(foreignMarkup, "category"))
                .description(syndEntry.getDescription() != null ? trim(syndEntry.getDescription().getValue()) : null)
                .postalCodes(postalCodes == null ? "" : postalCodes)
                .locations(extractFromForeignMarkup(foreignMarkup, "locations"))
                .channel(channel)
                .build();
    }

    private static String extractFromForeignMarkup(final List<? extends Element> elements, final String elementName) {
        return elements.stream()
                .filter(element -> Objects.equals(element.getName(), elementName))
                .flatMap(element -> element.getContent().stream())
                .map(Content::getValue)
                .filter(Objects::nonNull)
                .findFirst()
                .map(OutagesFetcherRestAdapter::trim)
                .orElse(null);

    }


    private static String link(final SyndEntry entry) {
        final Stream<Supplier<Optional<String>>> extractors = Stream.of(
                () -> extractLinkFromEnclosures(entry),
                () -> extractLinkFromLink(entry),
                () -> extractLinkFromLinks(entry)
        );
        return extractors.map(Supplier::get)
                .flatMap(Optional::stream)
                .findFirst()
                .orElse(null);
    }

    private static Optional<String> extractLinkFromEnclosures(final SyndEntry entry) {
        return entry.getEnclosures().stream()
                .map(SyndEnclosure::getUrl)
                .findFirst();
    }

    private static Optional<String> extractLinkFromLink(final SyndEntry entry) {
        final String entryLink = entry.getLink();
        if (!Strings.isBlank(entryLink)) {
            return Optional.of(entryLink.trim());
        }
        return Optional.empty();
    }

    private static Optional<String> extractLinkFromLinks(final SyndEntry entry) {
        return entry.getLinks().stream()
                .filter(link -> !Strings.isBlank(link.getHref()))
                .findFirst()
                .map(link -> trim(link.getHref()));
    }

    private static Optional<SyndFeed> toFeed(final InputStream inputStream) {
        try {
            final SyndFeedInput input = new SyndFeedInput();
            return Optional.of(input.build(new XmlReader(inputStream)));
        } catch (final Exception e) {
            log.warn("Unable to process feed ", e);
            return Optional.empty();
        }
    }

    private static String trim(final String string) {
        return string == null ? "" : string.trim();
    }

}
