$(window).resize(function () {
    $('body').css('padding-top', parseInt($('#main-navbar').css("height")));
});

// Deprecated
// $(window).load(function () {
//     $('body').css('padding-top', parseInt($('#main-navbar').css("height")));
// });

$(window).on('load', function () {
    $('body').css('padding-top', parseInt($('#main-navbar').css("height")));
});