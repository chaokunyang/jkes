
$("#request_body_search_form").submit(function (event) {
    event.preventDefault();

    var $form = $(this),
        path = $form.find("#path").val(),
        request_body = $form.find("#request_body").val();

    path = window.location.pathname + "/" + path;

    var target_container = $('#search_result_container');

    if(path.indexOf("_search") == -1) {
        $.get(path, function (dada) {
            fillResult(dada);
        });
    }else {
        $.ajax({
            url: path,
            type: 'post',
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                fillResult(data);
            },
            data: JSON.stringify(request_body)
        });
    }

    var fillResult = function (data) {
        var json = JSON.stringify(data, null, 4);
        var code = $("<code contenteditable='true'/>").append(json);
        var pre = $("<pre/>").append(code);

        target_container.empty();
        target_container.append(
            pre
        );
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    }
});

